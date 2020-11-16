package com.valueplus.domain.service.concretes;

import com.valueplus.app.exception.ValuePlusException;
import com.valueplus.domain.model.AccountSummary;
import com.valueplus.domain.service.abstracts.SummaryService;
import com.valueplus.persistence.entity.ProductOrder;
import com.valueplus.persistence.entity.Transaction;
import com.valueplus.persistence.entity.User;
import com.valueplus.persistence.repository.DeviceReportRepository;
import com.valueplus.persistence.repository.ProductOrderRepository;
import com.valueplus.persistence.repository.TransactionRepository;
import com.valueplus.persistence.repository.UserRepository;
import com.valueplus.domain.util.FunctionUtil;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static com.valueplus.domain.enums.OrderStatus.COMPLETED;

@RequiredArgsConstructor
@Service
public class DefaultSummaryService implements SummaryService {

    private final UserRepository userRepository;
    private final ProductOrderRepository productOrderRepository;
    private final TransactionRepository transactionRepository;
    private final DeviceReportRepository deviceReportRepository;

    @Override
    public AccountSummary getSummary(User user) throws ValuePlusException {
        Integer totalDownloads = 0;
        Integer activeUsers = deviceReportRepository.countAllByAgentCode(user.getAgentCode()).intValue();
        Integer totalAgents = Optional.ofNullable(user.getAgentCode())
                .map(a -> Strings.isNotBlank(a) ? 1 : 0)
                .orElse(0);

        ProductSummary productSummary = calculateProductSummary(productOrderRepository.findByUser_idAndStatus(user.getId(), COMPLETED));
        TransactionSummary transactionSummary = calculateTransactionSummary(transactionRepository.findAllByUser_Id(user.getId()));

        return AccountSummary.builder()
                .totalAgents(totalAgents)
                .totalProductSales(productSummary.getTotalProductSales())
                .totalProductAgentProfits(productSummary.getTotalProductAgentProfits())
                .totalApprovedWithdrawals(transactionSummary.getTotalApprovedWithdrawals())
                .totalPendingWithdrawal(transactionSummary.getTotalPendingWithdrawal())
                .pendingWithdrawalCount(transactionSummary.getPendingWithdrawalCount())
                .totalActiveUsers(activeUsers)
                .totalDownloads(totalDownloads)
                .build();
    }

    @Override
    public AccountSummary getSummaryAllUsers() throws ValuePlusException {
        Integer totalDownloads = 0;
        Integer activeUsers = ((Long) deviceReportRepository.count()).intValue();
        Integer totalAgents = userRepository.countAllByAgentCodeIsNotNull().intValue();

        ProductSummary productSummary = calculateProductSummary(productOrderRepository.findByStatus(COMPLETED));
        TransactionSummary transactionSummary = calculateTransactionSummary(transactionRepository.findAll());

        return AccountSummary.builder()
                .totalAgents(totalAgents)
                .totalProductSales(productSummary.getTotalProductSales())
                .totalProductAgentProfits(productSummary.getTotalProductAgentProfits())
                .totalApprovedWithdrawals(transactionSummary.getTotalApprovedWithdrawals())
                .totalPendingWithdrawal(transactionSummary.getTotalPendingWithdrawal())
                .pendingWithdrawalCount(transactionSummary.getPendingWithdrawalCount())
                .totalActiveUsers(activeUsers)
                .totalDownloads(totalDownloads)
                .build();
    }

    private ProductSummary calculateProductSummary(List<ProductOrder> productOrders) {
        AtomicReference<BigDecimal> totalProductAgentProfits = new AtomicReference<>(FunctionUtil.setScale(BigDecimal.ZERO));
        AtomicReference<BigDecimal> totalProductSales = new AtomicReference<>(FunctionUtil.setScale(BigDecimal.ZERO));

        productOrders.forEach(po -> {
            BigDecimal sellingPrice = FunctionUtil.setScale(po.getSellingPrice());
            BigDecimal productPrice = FunctionUtil.setScale(po.getProduct().getPrice());
            BigDecimal profit = FunctionUtil.setScale(sellingPrice.subtract(productPrice)).multiply(BigDecimal.valueOf(po.getQuantity()));

            BigDecimal sellingAmount = sellingPrice.multiply(BigDecimal.valueOf(po.getQuantity()));
            totalProductAgentProfits.accumulateAndGet(profit, BigDecimal::add);
            totalProductSales.accumulateAndGet(sellingAmount, BigDecimal::add);
        });

        return new ProductSummary(totalProductSales.get(), totalProductAgentProfits.get());
    }


    private TransactionSummary calculateTransactionSummary(List<Transaction> transactions) {
        AtomicReference<BigDecimal> totalApprovedWithdrawals = new AtomicReference<>(FunctionUtil.setScale(BigDecimal.ZERO));
        AtomicReference<BigDecimal> totalPendingWithdrawal = new AtomicReference<>(FunctionUtil.setScale(BigDecimal.ZERO));
        AtomicReference<Integer> pendingWithdrawalCount = new AtomicReference<>(0);

        transactions.forEach(tr -> {
            if (isSuccessfulTransaction(tr)) {
                totalApprovedWithdrawals.accumulateAndGet(tr.getAmount(), BigDecimal::add);
            } else if (isPendingTransaction(tr)) {
                totalPendingWithdrawal.accumulateAndGet(tr.getAmount(), BigDecimal::add);
                pendingWithdrawalCount.updateAndGet(v -> v + 1);
            }
        });

        return new TransactionSummary(
                totalApprovedWithdrawals.get(),
                totalPendingWithdrawal.get(),
                pendingWithdrawalCount.get());
    }

    private boolean isSuccessfulTransaction(Transaction tr) {
        return "success".equalsIgnoreCase(tr.getStatus());
    }

    private boolean isPendingTransaction(Transaction tr) {
        return !isSuccessfulTransaction(tr) && !"error".equalsIgnoreCase(tr.getStatus()) && !"failed".equalsIgnoreCase(tr.getStatus());
    }

    @Value
    private static class TransactionSummary {
        BigDecimal totalApprovedWithdrawals;
        BigDecimal totalPendingWithdrawal;
        Integer pendingWithdrawalCount;
    }

    @Value
    private static class ProductSummary {
        BigDecimal totalProductSales;
        BigDecimal totalProductAgentProfits;
    }
}
