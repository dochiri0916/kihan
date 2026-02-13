# 도메인 모델

---

## 공통 속성 (BaseEntity)

모든 엔티티는 아래 공통 속성을 가집니다.

- `createdAt`:`LocalDateTime` - 생성 일시
- `updatedAt`:`LocalDateTime` - 수정 일시
- `createdBy`:`Long` - 생성자(사용자 ID)
- `updatedBy`:`Long` - 수정자(사용자 ID)
- `deletedAt`:`LocalDateTime` - 삭제 일시 (논리 삭제)

---

## [마감 애그리거트]

### 마감 (Deadline)

_Aggregate Root_

**속성**

- `id`:`Long` - 식별자
- `title`:`String` - 제목
- `description`:`String` - 설명
- `type`:`DeadlineType` - 유형 (ONE_TIME, RECURRING)
- `dueDate`:`LocalDateTime` - 마감일 (단발 마감용, nullable)
- `recurrenceRule`:`RecurrenceRule` - 반복 규칙 (반복 마감용, nullable)

**행위**

- `static register(title, description, type, dueDate, recurrenceRule)` - 마감을 등록한다.
- `changeTitle(newTitle)` - 제목을 변경한다.
- `changeDescription(newDescription)` - 설명을 변경한다.
- `markAsCompleted()` - 마감을 완료로 표시한다.

**규칙**

- 마감일은 현재 시각 이후여야 한다.

**예외**

`DeadlineNotFoundException` 
- 마감을 찾을 수 없음
- HTTP 404 `Not Found`

`InvalidDeadlineRuleException`
- 마감 유형과 속성 조합이 올바르지 않음
- HTTP 400 `Bad Request`

### 반복 규칙 (RecurrenceRule)

_Value Object_

**속성**

- `pattern`:`RecurrencePattern` - 반복 유형 (DAILY, WEEKLY, MONTHLY, YEARLY)
- `interval`:`int` - 반복 간격
- `startDate`:`LocalDate`- 시작일
- `endDate`:`LocalDate` - 종료일 (선택)

**규칙**

- `interval`은 1 이상의 값이어야 한다.
- `endDate`가 설정된 경우, `endDate`는 `startDate` 이후여야 한다.

### 실행 (Execution)

_Entity_

**속성**

- `id`:`Long` - 식별자
- `deadlineId`:`Long` - 마감 식별자
- `userId`:`Long` - 사용자 식별자
- `scheduledDate`:`LocalDate` - 수행 예정일
- `status`:`ExecutionStatus` - 상태 (READY, DONE, DELAYED)
- `completedAt`:`LocalDateTime` - 완료 일시 (nullable)

**행위**

- `markAsDone()` - 실행을 완료로 표시한다.
- `markAsDelayed()` - 실행을 지연으로 표시한다.

**규칙**

- 동일한 `deadlineId`와 `scheduledDate` 조합의 실행은 중복될 수 없다.

**예외**

`ExecutionNotFoundException`
- 실행을 찾을 수 없음
- HTTP 404 `Not Found`

`ExecutionAlreadyCompletedException`
- 이미 완료된 실행
- HTTP 400 `Bad Request`

---

