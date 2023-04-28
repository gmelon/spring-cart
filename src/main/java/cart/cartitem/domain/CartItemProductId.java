package cart.cartitem.domain;

import java.util.Objects;

public class CartItemProductId {
    private final Long value;

    public CartItemProductId(Long value) {
        this.value = value;
        validatePositive(this.value);
    }

    private void validatePositive(Long value) {
        if (value < 0) {
            throw new IllegalArgumentException("상품 ID는 0 이상이어야 합니다.");
        }
    }

    public Long getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItemProductId that = (CartItemProductId) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}