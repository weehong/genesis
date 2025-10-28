package com.resetrix.horaion;

import com.resetrix.horaion.shared.securities.SecurityConfigurationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(SecurityConfigurationTest.class)
class HoraionApplicationTests {

    @Test
    void contextLoads() {
    }

}
