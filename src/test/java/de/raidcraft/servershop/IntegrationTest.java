package de.raidcraft.servershop;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.*;

public class IntegrationTest {

    private ServerMock server;
    private ServerShop plugin;

    @BeforeEach
    void setUp() {

        this.server = MockBukkit.mock();
        this.plugin = MockBukkit.load(ServerShop.class);
    }

    @AfterEach
    void tearDown() {

        MockBukkit.unmock();
    }

    @Nested
    @DisplayName("Commands")
    class Commands {

        private Player player;

        @BeforeEach
        void setUp() {
            player = server.addPlayer();
        }

        @Nested
        @DisplayName("/template:admin")
        class AdminCommands {

            @Nested
            @DisplayName("foo bar")
            class add {

                @Test
                @DisplayName("should work")
                void shouldWork() {

                    server.dispatchCommand(server.getConsoleSender(),"rc:template add foo " + player.getName() + " bar");
                }
            }
        }
    }
}
