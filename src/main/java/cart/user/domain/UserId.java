package cart.user.domain;

import java.util.Objects;

public class UserId {
    private final Long value;

    public UserId(Long value) {
        this.value = value;
        validatePositive(this.value);
    }

    private void validatePositive(Long value) {
        if (value < 0) {
            throw new IllegalArgumentException("사용자 ID는 0 이상이어야 합니다.");
        }
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}