package com.resetrix.genesis;

import com.resetrix.genesis.shared.securities.SecurityConfigurationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(SecurityConfigurationTest.class)
class GenesisApplicationTests {

    @Test
    void contextLoads() {
    }

}
