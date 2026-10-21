package dev.fknemi.ledger.common.idempotency;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Intercepts requests with an Idempotency-Key header.
 * If the key has been seen before, replays the stored response.
 * TODO: implement response capturing and replay.
 */
@Component
@RequiredArgsConstructor
public class IdempotencyInterceptor implements HandlerInterceptor {

    static final String HEADER = "Idempotency-Key";

    private final IdempotencyRepository idempotencyRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String key = request.getHeader(HEADER);
        if (key == null || key.isBlank()) return true; // no key → pass through

        return idempotencyRepository.findById(key)
                .map(record -> {
                    try {
                        response.setStatus(record.getHttpStatus());
                        response.setContentType("application/json");
                        response.getWriter().write(record.getResponseBody());
                        return false; // short-circuit
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .orElse(true); // not seen yet → proceed
    }
}
