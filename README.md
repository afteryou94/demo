# 🚀 Spring Boot 기반 커뮤니티 게시판 프로젝트
> **Spring Boot와 JPA를 활용한 CRUD 및 보안 강화 커뮤니티 웹 서비스**
>
> <img width="1878" height="965" alt="2026-04-06 17_03_43-demo – BoardService java  demo main" src="https://github.com/user-attachments/assets/8ae64bb2-126a-43d3-abc2-ecf9c747c0d8" />

목차

# 1. 프로젝트 개요
- **개발 기간**: 2026.02 ~ 2026.03
- **주요 목적**: CRUD 게시판을 바탕으로 Spring Security를 통한 인증 보안 강화 및 JPA 기반 동적 검색 기능 등 구현 연습
## 1. 프로젝트 기능
- **게시판** - 권한 별 CRUD 기능, 조회수, 페이징 및 검색 처리
- **사용자** - Security를 통한 회원가입 및 로그인, OAuth 2.0 네이버, 구글 로그인, 회원 정보 수정, 유효성 검사 및 중복 검사, 이메일 인증번호 발송, 소셜 계정 첫 로그인 시 닉네임 설정
- **댓글** - 권한 별 CRUD 기능
## 2. **핵심 기술**
### 2-1. 백엔드
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

### 2-2. 프론트엔드
- Html
- JavaScript
- Bootstrap 5.3.2
- Thymeleaf
- AJAX
# 2. 실행 화면
게시글 관련
## **1. 게시글 목록**

   <img width="1280" height="713" alt="image" src="https://github.com/user-attachments/assets/84f5f09c-1680-48cf-9894-4a983036f3c2" />
   
전체 목록을 페이징 처리하여 조회가 가능하다.

## **2. 게시글 등록**

<img width="1021" height="1001" alt="image" src="https://github.com/user-attachments/assets/271a4923-7a92-4c21-a4fd-6bfa0ddc37c2" />

비회원은 2~10자 ID, 수정,삭제에 사용할 비밀번호(최소4자리)를 입력해야 게시글 등록이 가능하다. 작성 후 목록 화면으로 redirect

<img width="1001" height="838" alt="image" src="https://github.com/user-attachments/assets/7fa5850a-b865-432e-a821-dc56b7667145" />

회원은 세션에 입력된 이메일 정보로 인해 제목과 내용을 자유롭게 작성할 수 있다. 작성 후 목록 화면으로 redirect

## **3. 게시글 상세보기**

<img width="1280" height="840" alt="image" src="https://github.com/user-attachments/assets/0e3fbda5-1b06-47b7-a396-8de0577c6442" />

비회원 세션

<img width="1280" height="824" alt="image" src="https://github.com/user-attachments/assets/56a27196-6956-439c-825c-e79d5cb01797" />

회원은 오른쪽 상단에 닉네임이 보인다.

## **4. 게시글 수정화면**

<img width="972" height="994" alt="image" src="https://github.com/user-attachments/assets/66748a1f-765f-471a-9068-e2852115575f" />

<img width="1107" height="842" alt="image" src="https://github.com/user-attachments/assets/2b223b6b-7a68-4b14-a434-9e40cf49497b" />

<img width="1280" height="814" alt="image" src="https://github.com/user-attachments/assets/7a0c4f70-f421-441b-ae23-45399a55be2a" />

비회원은 등록할 시 입력했던 비밀번호를 입력해야 수정이 가능하다.

<img width="1280" height="827" alt="image" src="https://github.com/user-attachments/assets/b7b99cb2-49a6-44db-99b5-bfd24ce76bd5" />

<img width="1064" height="728" alt="image" src="https://github.com/user-attachments/assets/9de3b5d7-365c-4897-9c8e-f6c67470b596" />

<img width="1280" height="810" alt="image" src="https://github.com/user-attachments/assets/2876b5c4-fbfe-4e1e-a91e-43410355f71a" />

회원은 본인의 글을 자유롭게 수정할 수 있다. 수정하고 나면 게시글 상세보기 화면으로 redirect한다.

## **5. 게시글 삭제화면**

<img width="1280" height="832" alt="image" src="https://github.com/user-attachments/assets/919de28d-fc9d-4de1-a45a-9768822e4126" />
<img width="717" height="634" alt="image" src="https://github.com/user-attachments/assets/d89e4e9a-e1a7-45ca-85ac-4f44414f2e48" />

비회원은 비밀번호 입력창에 작성에 사용했던 비밀번호를 입력해야 삭제가 가능하다. 삭제 후 목록 리스트로 redirect

<img width="1280" height="763" alt="image" src="https://github.com/user-attachments/assets/a563ccf0-fc4e-480b-8e37-eada6d6365a9" />

회원은 본인의 글을 자유롭게 삭제할 수 있다. 삭제 후 목록 리스트로 redirect

## **6. 게시글 검색화면**

<img width="1280" height="596" alt="image" src="https://github.com/user-attachments/assets/11326f83-b638-4ce5-aa99-ccb82c2c905e" />

검색 키워드를 입력하고 검색을 누르면 키워드가 포함된 게시글이 나타난다. 옵션은 1. 제목+내용 2. 제목 3. 내용 4. 작성자

<img width="1280" height="814" alt="image" src="https://github.com/user-attachments/assets/a66c3b2b-06eb-48ce-bb1c-c3a967c0d136" />
<img width="1280" height="456" alt="image" src="https://github.com/user-attachments/assets/c1058213-a92c-4e67-bcdb-4dbe4f78e37b" />

회원 관련
## **1. 회원가입 화면**

<img width="625" height="816" alt="2026-04-06 22_01_11-demo – save html  demo main" src="https://github.com/user-attachments/assets/acbd76d6-3d34-48f4-8a39-6e23e18f41bd" />
<img width="576" height="975" alt="2026-04-06 22_07_51-demo – save html  demo main" src="https://github.com/user-attachments/assets/4467f820-c5c1-4328-a09b-f99c86efa509" />

유효성 검사 및 중복확인를 통과해야 하며 메일을 수신할 수 있는 이메일을 입력해야한다.

<img width="591" height="1042" alt="image" src="https://github.com/user-attachments/assets/fd44636f-7c04-42d3-9ecd-1245e5ff1c28" />

DB에 있는 이메일을 입력하면 중복으로 인해 불가능한 모습

<img width="596" height="952" alt="2026-04-06 22_02_32-demo – save html  demo main" src="https://github.com/user-attachments/assets/f4a3d697-b299-47eb-9008-a7dcd2de50ae" />
<img width="596" height="899" alt="2026-04-06 22_02_42-demo – save html  demo main" src="https://github.com/user-attachments/assets/72fcd401-779b-4822-9560-0c6ea9d8658c" />
<img width="582" height="880" alt="2026-03-23 20_52_27-Inbox (9) - teraafteryou5@gmail com - Gmail — Mozilla Firefox" src="https://github.com/user-attachments/assets/a60dd9ea-fc05-4000-9eb2-6238a77abc1b" />

인증 시간 3분이 지나면 재발송 버튼을 눌러서 새로운 인증번호를 입력해야한다.

## **2. 로그인 화면**

<img width="549" height="792" alt="image" src="https://github.com/user-attachments/assets/60fab1af-6d45-4b1a-9565-e93d8500e9f5" />
<img width="592" height="931" alt="image" src="https://github.com/user-attachments/assets/f6013bb3-1cc7-400f-b46a-7516ce1eeaf4" />

아이디 또는 비밀번호 일치하지 않을 때 로그인을 시도한 모습

### **2.1 OAuth 2.0 소셜 로그인 화면
<img width="1280" height="632" alt="image" src="https://github.com/user-attachments/assets/71478aa7-3702-414c-a28a-42190adc2fe0" />

구글 로그인

<img width="960" height="783" alt="image" src="https://github.com/user-attachments/assets/969e6420-fccf-4158-b226-7ec439c9d6b8" />

네이버 로그인

## **3. 회원정보 수정**
이메일(readonly), 이름(소셜 계정 전용), 닉네임과 비밀번호 변경 가능, 완료하면 게시판 목록으로 redirect





