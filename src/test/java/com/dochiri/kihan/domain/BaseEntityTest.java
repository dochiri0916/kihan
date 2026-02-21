package com.dochiri.kihan.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("BaseEntity 도메인 테스트")
class BaseEntityTest {

    @Test
    @DisplayName("delete 호출 시 삭제 시각이 저장되고 삭제 상태가 된다")
    void shouldSetDeletedAtAndMarkAsDeletedWhenDeleteIsCalled() {
        TestEntity entity = new TestEntity();
        LocalDateTime deletedAt = LocalDateTime.of(2026, 2, 21, 1, 0);

        entity.delete(deletedAt);

        assertTrue(entity.isDeleted());
        assertNotNull(entity.getDeletedAt());
        assertEquals(deletedAt, entity.getDeletedAt());
    }

    @Test
    @DisplayName("delete에 null 전달 시 예외가 발생한다")
    void shouldThrowWhenDeleteIsCalledWithNull() {
        TestEntity entity = new TestEntity();

        assertThrows(NullPointerException.class, () -> entity.delete(null));
    }

    @Test
    @DisplayName("restore 호출 시 삭제 상태가 해제된다")
    void shouldClearDeletedStateWhenRestoreIsCalled() {
        TestEntity entity = new TestEntity();
        entity.delete(LocalDateTime.of(2026, 2, 21, 1, 0));

        entity.restore();

        assertFalse(entity.isDeleted());
    }

    @Test
    @DisplayName("삭제되지 않은 상태에서 restore를 호출해도 예외 없이 유지된다")
    void shouldKeepNotDeletedStateWhenRestoreIsCalledWithoutDelete() {
        TestEntity entity = new TestEntity();

        entity.restore();

        assertFalse(entity.isDeleted());
    }

    private static class TestEntity extends BaseEntity {
    }

}
