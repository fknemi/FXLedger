package dev.fknemi.ledger.reversal;

import dev.fknemi.ledger.transaction.dto.TransactionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class ReversalController {

    private final ReversalService reversalService;

    @PostMapping("/{id}/reverse")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse reverse(@PathVariable UUID id) {
        return reversalService.reverse(id);
    }
}
