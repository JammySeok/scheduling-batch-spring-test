## 이 프로젝트는 Spring 스케줄러의 동작 방식과 멀티 인스턴스 환경에서의 도입 방법을 학습하기 위해 만들어진 토이 프로젝트입니다.


## 스케줄러 학습 프로젝트

### 🛠 기술 스택

#### Language
![Java](https://img.shields.io/badge/Java%2021-007396?style=for-the-badge&logo=openjdk&logoColor=white)

#### ⚙ Framework & Library
![Spring Boot](https://img.shields.io/badge/SpringBoot%204.1-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)


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


## 코드 용어
- `@EnableScheduling`: 스케줄링 기능을 켜는 설정 어노테이션
- `@Scheduled`: 이 메서드를 예약 실행하라는 표시 (반환값 `void`, 파라미터 없음)
  - `fixedRate`: 이전 실행의 **시작** 시각 기준으로 일정 간격마다 실행
  - `fixedDelay`: 이전 실행이 **끝난** 시각 기준으로 일정 시간 뒤 실행
  - `cron`: 달력 기준 특정 시각에 실행


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

## 참고 자료
- [Spring Framework - Task Execution and Scheduling](https://docs.spring.io/spring-framework/reference/integration/scheduling.html)
- [Spring Boot - Task Execution and Scheduling](https://docs.spring.io/spring-boot/reference/features/task-execution-and-scheduling.html)
- [Spring Guide - Scheduling Tasks](https://spring.io/guides/gs/scheduling-tasks)