package jammy.schedulingbatchspringtest.batch.domain;

import java.time.LocalDateTime;

public record Order(
        Long id,
        int amount,
        String status,
        LocalDateTime orderedDate
) {}
