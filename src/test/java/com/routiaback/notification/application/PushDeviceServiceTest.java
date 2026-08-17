package com.routiaback.notification.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.routiaback.auth.application.port.UserRepositoryPort;
import com.routiaback.auth.domain.User;
import com.routiaback.global.error.ApiException;
import com.routiaback.global.error.ErrorCode;
import com.routiaback.notification.application.port.PushDeviceRepositoryPort;
import com.routiaback.notification.domain.PushDevice;
import com.routiaback.notification.domain.PushPlatform;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PushDeviceServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-17T00:00:00Z");
    private final FakeUserRepository users = new FakeUserRepository();
    private final FakeDeviceRepository devices = new FakeDeviceRepository();
    private final PushDeviceService service = new PushDeviceService(
            users, devices, Clock.fixed(NOW, ZoneOffset.UTC));

    @BeforeEach
    void setUp() {
        users.users.add(User.create("one@example.com", "hash", "One", NOW).withId(1L));
        users.users.add(User.create("two@example.com", "hash", "Two", NOW).withId(2L));
    }

    @Test
    void registersAndReactivatesSameUserTokenWithoutDuplicateRow() {
        PushDeviceService.DeviceResult first = service.register(1L, 1L, " token-1 ", PushPlatform.WEB);
        service.deactivate(1L, 1L, first.id());
        PushDeviceService.DeviceResult reactivated = service.register(1L, 1L, "token-1", PushPlatform.WEB);

        assertThat(devices.devices).hasSize(1);
        assertThat(reactivated.id()).isEqualTo(first.id());
        assertThat(reactivated.active()).isTrue();
    }

    @Test
    void transfersTokenOwnershipToCurrentlyAuthenticatedUser() {
        service.register(1L, 1L, "shared-token", PushPlatform.WEB);

        service.register(2L, 2L, "shared-token", PushPlatform.WEB);

        assertThat(devices.devices).singleElement().extracting(PushDevice::userId).isEqualTo(2L);
    }

    @Test
    void rejectsOtherUserAndInvalidToken() {
        assertThatThrownBy(() -> service.register(1L, 2L, "token", PushPlatform.WEB))
                .isInstanceOf(ApiException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.USER_DATA_ACCESS_DENIED);
        assertThatThrownBy(() -> service.register(1L, 1L, " ", PushPlatform.WEB))
                .isInstanceOf(ApiException.class).extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_PUSH_DEVICE);
    }

    private static class FakeUserRepository implements UserRepositoryPort {
        private final List<User> users = new ArrayList<>();
        @Override public boolean existsByEmail(String email) { return false; }
        @Override public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override public Optional<User> findById(Long id) {
            return users.stream().filter(user -> user.id().equals(id)).findFirst();
        }
        @Override public User save(User user) { users.add(user); return user; }
    }

    private static class FakeDeviceRepository implements PushDeviceRepositoryPort {
        private final List<PushDevice> devices = new ArrayList<>();
        @Override public Optional<PushDevice> findByToken(String token) {
            return devices.stream().filter(device -> device.token().equals(token)).findFirst();
        }
        @Override public Optional<PushDevice> findByIdAndUserId(Long id, Long userId) {
            return devices.stream().filter(device -> device.id().equals(id) && device.userId().equals(userId)).findFirst();
        }
        @Override public List<PushDevice> findAllActiveByUserId(Long userId) {
            return devices.stream().filter(device -> device.userId().equals(userId) && device.active()).toList();
        }
        @Override public PushDevice save(PushDevice device) {
            PushDevice saved = device.id() == null
                    ? new PushDevice((long) devices.size() + 1, device.userId(), device.token(), device.platform(),
                            device.active(), device.lastSeenAt(), device.createdAt(), device.updatedAt())
                    : device;
            devices.removeIf(existing -> existing.id().equals(saved.id()));
            devices.add(saved);
            return saved;
        }
    }
}
