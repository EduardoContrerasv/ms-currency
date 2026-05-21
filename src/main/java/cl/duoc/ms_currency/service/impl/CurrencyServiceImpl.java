package cl.duoc.ms_currency.service.impl;

import cl.duoc.ms_currency.dto.CurrencyRequestDto;
import cl.duoc.ms_currency.dto.CurrencyResponseDto;
import cl.duoc.ms_currency.enums.CurrencyType;
import cl.duoc.ms_currency.model.UserCurrency;
import cl.duoc.ms_currency.repository.CurrencyRepository;
import cl.duoc.ms_currency.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CurrencyServiceImpl implements CurrencyService {

    private final CurrencyRepository repository;

    @Override
    public void addCurrency(Long userId, CurrencyRequestDto dto) {

        UserCurrency wallet = repository.findByUserIdAndCurrencyType(userId, dto.getCurrencyType())
                .orElseGet(() -> {
                    UserCurrency newWallet = new UserCurrency();
                    newWallet.setUserId(userId);
                    newWallet.setCurrencyType(dto.getCurrencyType());
                    newWallet.setAmount(0);
                    return newWallet;
                });

        wallet.setAmount(wallet.getAmount() + dto.getAmount());
        repository.save(wallet);
    }

    @Override
    public void deductCurrency(Long userId, CurrencyRequestDto dto) {

        UserCurrency wallet = repository.findByUserIdAndCurrencyType(userId, dto.getCurrencyType())
                .orElseThrow(() -> new RuntimeException("Billetera no encontrada para esta moneda"));

        if (wallet.getAmount() < dto.getAmount()) {
            throw new RuntimeException("Fondos insuficientes. Tienes: " + wallet.getAmount() + " " + dto.getCurrencyType());
        }

        wallet.setAmount(wallet.getAmount() - dto.getAmount());
        repository.save(wallet);
    }

    @Override
    public List<CurrencyResponseDto> getAllBalances(Long userId) {
        List<UserCurrency> rawWallets = repository.findByUserId(userId);

        return rawWallets.stream()
                .map(wallet -> new CurrencyResponseDto(
                        wallet.getCurrencyType(),
                        wallet.getAmount()
                ))
                .toList();
    }

    @Override
    public int getSpecificBalance(Long userId, CurrencyType currencyType) {
        return repository.findByUserIdAndCurrencyType(userId, currencyType)
                .map(UserCurrency::getAmount)
                .orElse(0);
    }

}
