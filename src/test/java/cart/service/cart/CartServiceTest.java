package cart.service.cart;

import cart.domain.cart.Cart;
import cart.domain.user.User;
import cart.infrastructure.dao.CartDao;
import cart.infrastructure.security.AccessDeniedException;
import cart.web.cart.dto.CartAddRequestDto;
import cart.web.cart.dto.CartResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Sql(scripts = "classpath:schema.sql")
@SpringBootTest
class CartServiceTest {

    private static final User USER_1 = User.builder()
            .id(1L)
            .email("a@a.com")
            .password("passwordA")
            .build();

    @Autowired
    private CartService cartService;
    @Autowired
    private CartDao cartDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // reset tables
        jdbcTemplate.update("DELETE FROM cart");
        jdbcTemplate.update("DELETE FROM product");
        jdbcTemplate.update("DELETE FROM users");

        // user insert
        jdbcTemplate.update("INSERT INTO users(email, password) VALUES('a@a.com', 'passwordA')"); // id = 1
        jdbcTemplate.update("INSERT INTO users(email, password) VALUES('b@b.com', 'passwordB')"); // id = 2

        // product insert
        jdbcTemplate.update("INSERT INTO product(name, image_url, price) VALUES('상품A', 'image.com/imageA', 10000)"); // id = 1
        jdbcTemplate.update("INSERT INTO product(name, image_url, price) VALUES('상품B', 'image.com/imageB', 20000)"); // id = 2
        jdbcTemplate.update("INSERT INTO product(name, image_url, price) VALUES('상품C', 'image.com/imageC', 30000)"); // id = 3

        // cart insert
        jdbcTemplate.update("INSERT INTO cart(user_id, product_id) VALUES(1, 1)");
        jdbcTemplate.update("INSERT INTO cart(user_id, product_id) VALUES(1, 2)");
        jdbcTemplate.update("INSERT INTO cart(user_id, product_id) VALUES(2, 1)");
    }

    @Test
    void findAll() {
        // given, when
        List<CartResponseDto> cartResponseDtos = cartService.findAll(USER_1);

        // then
        assertThat(cartResponseDtos).flatExtracting(CartResponseDto::getProductId)
                .containsExactly(1L, 2L);
        assertThat(cartResponseDtos).flatExtracting(CartResponseDto::getName)
                .containsExactly("상품A", "상품B");
        assertThat(cartResponseDtos).flatExtracting(CartResponseDto::getImageUrl)
                .containsExactly("image.com/imageA", "image.com/imageB");
        assertThat(cartResponseDtos).flatExtracting(CartResponseDto::getPrice)
                .containsExactly(10000, 20000);
    }

    @Test
    void add_valid() {
        // given
        CartAddRequestDto requestDto = new CartAddRequestDto(3L);

        // when
        Long addedCartId = cartService.add(USER_1, requestDto);

        // then
        Cart foundCart = assertDoesNotThrow(() -> cartDao.findById(addedCartId).get());
        assertThat(foundCart.getUserId()).isEqualTo(USER_1.getId());
        assertThat(foundCart.getProductId()).isEqualTo(requestDto.getProductId());
    }

    @Test
    void add_invalid_nonexistent_product() {
        // given
        CartAddRequestDto requestDto = new CartAddRequestDto(4L);

        // when, then
        assertThatThrownBy(() -> cartService.add(USER_1, requestDto))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void remove_valid() {
        // given, when
        Long removedCartId = cartService.remove(USER_1, 1L);

        // then
        assertThat(cartDao.findById(removedCartId)).isEmpty();
    }

    @Test
    void remove_invalid_nonexistent_cart() {
        assertThatThrownBy(() -> cartService.remove(USER_1, 4L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void remove_invalid_access_denied() {
        assertThatThrownBy(() -> cartService.remove(USER_1, 3L))
                .isInstanceOf(AccessDeniedException.class);
    }
}