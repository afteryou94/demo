# 🚀 Spring Boot 기반 커뮤니티 게시판 프로젝트
> **Spring Boot와 JPA를 활용한 CRUD 및 보안 강화 커뮤니티 웹 서비스**
>
> <img width="1878" height="965" alt="2026-04-06 17_03_43-demo – BoardService java  demo main" src="https://github.com/user-attachments/assets/8ae64bb2-126a-43d3-abc2-ecf9c747c0d8" />

목차

# 프로젝트 개요
- **개발 기간**: 2026.02 ~ 2026.03
- **주요 목적**: CRUD 게시판을 바탕으로 Spring Security를 통한 인증 보안 강화 및 JPA 기반 동적 검색 기능 등 구현 연습
## 프로젝트 기능
- **게시판** - 권한 별 CRUD 기능, 조회수, 페이징 및 검색 처리
- **사용자** - Security를 통한 회원가입 및 로그인, OAuth 2.0 네이버, 구글 로그인, 회원 정보 수정, 유효성 검사 및 중복 검사, 이메일 인증번호 발송, 소셜 계정 첫 로그인 시 닉네임 설정
- **댓글** - 권한 별 CRUD 기능
## **핵심 기술**
### 백엔드
**주요 프레임워크 / 라이브러리**
- Java 21
- SpringBoot 3.5.10
- JPA(Spring Data JPA)
- Spring security 6
- OAuth 2.0
- Spring Boot Starter Validation
- Lombok

**Build Tool**
- Gradle 8.14.3

**DataBase**
- MySQL 8.0.45

### 프론트엔드
- Html
- JavaScript
- Bootstrap 5.3.2
- Thymeleaf
- AJAX
