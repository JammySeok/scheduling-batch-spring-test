## 이 프로젝트는 Spring 스케줄러의 동작 방식과 멀티 인스턴스 환경에서의 도입 방법을 학습하기 위해 만들어진 토이 프로젝트입니다.


## 스케줄러 학습 프로젝트

### 기술 스택

#### Language
![Java](https://img.shields.io/badge/Java%2021-007396?style=for-the-badge&logo=openjdk&logoColor=white)

#### Framework & Library
![Spring Boot](https://img.shields.io/badge/SpringBoot%204.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![ShedLock](https://img.shields.io/badge/ShedLock%207.10-555555?style=for-the-badge)

#### Infra
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)


---

### 학습 단계

| 단계 | 내용 |
|---|---|
| 1 | `@Scheduled` 기본 (fixedRate / fixedDelay / cron) |
| 2 | 스케줄러 스레드 풀과 작업 간 블로킹 확인 |
| 3 | 타임존(`zone`)과 cron 표현식 |
| 4 | 인스턴스 2개 실행 → 중복 실행 문제 재현 |
| 5 | 분산 락으로 중복 실행 방지 |

---

# 스케줄러(Scheduler)

## 개념

### 스케줄러
- 정해진 시각이나 주기에 메서드를 자동으로 실행하는 기능
- ex) 매일 새벽 4시에 탈퇴 회원 데이터 정리, 10분마다 미발송 메일 재전송

### TaskScheduler
- Spring이 예약 작업을 실행하는 데 쓰는 실행기
- `@EnableScheduling`을 붙이면 Spring Boot가 자동으로 만들어 줌
- 기본 스레드는 **1개** → 서로 다른 작업끼리도 앞 작업이 끝날 때까지 대기
  - 동시에 돌아야 하는 작업이 있으면 `spring.task.scheduling.pool.size`를 늘림

### 타임존
- cron은 `zone` 미지정 시 **JVM 기본 타임존** 기준으로 해석됨
  - 로컬(KST)에서 잘 돌던 cron이 UTC 서버/컨테이너에서는 9시간 어긋남
- cron에는 항상 `zone`을 명시 (ex. `zone = "Asia/Seoul"`)

### 멀티 인스턴스 중복 실행
- 스케줄러는 인스턴스(JVM)마다 따로 동작 → 인스턴스가 N대면 같은 작업이 N번 실행됨
- 멱등하지 않은 작업(메일 발송, 금액 지급 등)은 결과가 틀어짐
  - 멱등(idempotent): 여러 번 실행해도 결과가 같은 성질

### 분산 락(Distributed Lock)
- 인스턴스들이 공유하는 저장소(Redis, DB)에 "실행 중" 표시를 남겨 한 대만 실행하게 하는 방법
- 락을 못 잡은 인스턴스는 기다리지 않고 **이번 실행을 건너뜀**
- ShedLock은 락만 제공 (재시도, 놓친 실행 보충, 작업 분산은 하지 않음)


## 코드 용어
- `@EnableScheduling`: 스케줄링 기능을 켜는 설정 어노테이션
- `@Scheduled`: 이 메서드를 예약 실행하라는 표시 (반환값 `void`, 파라미터 없음)
  - `fixedRate`: 이전 실행의 **시작** 시각 기준으로 일정 간격마다 실행
  - `fixedDelay`: 이전 실행이 **끝난** 시각 기준으로 일정 시간 뒤 실행
  - `cron`: 달력 기준 특정 시각에 실행
  - `zone`: cron을 해석할 타임존
- `@EnableSchedulerLock`: ShedLock 기능을 켜는 설정 어노테이션 (없으면 `@SchedulerLock`이 무시됨)
- `@SchedulerLock`: 실행 전에 분산 락을 잡으라는 표시
  - `name`: 락 이름. 같은 name끼리는 동시에 하나만 실행
  - `lockAtMostFor`: 실행 서버가 죽어도 이 시간 뒤 락 해제 (안전장치)
  - `lockAtLeastFor`: 작업이 빨리 끝나도 이 시간까지 락 유지 (서버 간 시계 오차 대비)


---

## cron 표현식

Spring cron은 리눅스 crontab(5필드)과 달리 **초부터 시작하는 6필드**이다.

```
┌─ 초 (0-59)
│ ┌─ 분 (0-59)
│ │ ┌─ 시 (0-23)
│ │ │ ┌─ 일 (1-31)
│ │ │ │ ┌─ 월 (1-12 또는 JAN-DEC)
│ │ │ │ │ ┌─ 요일 (0-7 또는 MON-SUN, 0과 7은 일요일)
│ │ │ │ │ │
* * * * * *
```

| 표현식 | 의미 |
|---|---|
| `0 0 4 * * *` | 매일 04:00:00 |
| `0 */10 * * * *` | 10분마다 |
| `0 0 9 * * MON-FRI` | 평일 09:00 |
| `0 0 0 1 * *` | 매월 1일 00:00 |


---

## 락 시간 설정 기준
- `작업 시간 < lockAtMostFor < 실행 주기`
  - lockAtMostFor가 작업 시간보다 짧으면 작업 도중 락이 풀려 다른 인스턴스가 중복 실행
- `lockAtLeastFor <= lockAtMostFor`


---

## 참고 자료
- [Spring Framework - Task Execution and Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Spring Boot - Task Execution and Scheduling](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html)
- [Spring Guide - Scheduling Tasks](https://spring.io/guides/gs/scheduling-tasks)
- [ShedLock GitHub](https://github.com/lukas-krecan/ShedLock)