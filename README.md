# Simple-Flash-Sale

![Next.js](https://img.shields.io/badge/Next.js_15-000000?style=for-the-badge&logo=nextdotjs&logoColor=white)
![React](https://img.shields.io/badge/React_19-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3.4-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

一個專注於實作**高併發場景下數據一致性**與**無感認證機制**的全端開發原型。本專案核心在於處理分散式環境中的 Race Condition，並確保極致的用戶認證體驗。

---

## 🛠 核心技術挑戰與解決方案 (Solutions)

### 1. 安全與持久化設計 (Security & Persistence)
* **安全實作：** 採用雙 Token JWT 架構，並強制使用 `HttpOnly` 與 `SameSite` Cookie 存儲，從根源防禦 XSS 攻擊。
* **資料一致性：** 透過 Spring Data JPA 結合 MySQL 交易機制，確保訂單資料在非同步環境下的最終一致性。

### 2. 認證失效時的併發請求處理 (Auth Race Condition)
* **挑戰：** 當 Access Token 過期時，頁面上的多個併發請求會同時觸發刷新 Token 的 API，導致 Token Rotation 機制因競爭失敗而觸發安全機制強制登出。
* **方案：** 實作 **Axios 異步緩衝隊列**。第一個觸發 401 的請求會鎖定狀態並執行 Refresh，將後續請求掛起 (Pending)，待新 Token 取得後自動補發所有隊列請求，實現透明的**無感滑動過期**。

### 3. 高併發下的流量削峰與原子操作 (Scalability & Atomic Control)
* **挑戰：** 瞬時湧入的大量搶購請求會造成資料庫 I/O 瓶頸，並導致傳統鎖機制下的庫存超賣。
* **方案：** * **Redis Lua Script**：將庫存檢查與扣減封裝為原子操作，在快取層即時過濾無效請求。
    * **RabbitMQ 削峰**：扣庫存成功後透過訊息隊列非同步寫入訂單，解耦「請求響應」與「資料落盤」，保護 MySQL 不受併發衝擊。

---

## 🏗 技術棧 (Tech Stack)

### Frontend
- **Next.js 15 (App Router)**: 利用 React 19 Server Components 提升首屏渲染效能。
- **Tailwind CSS**: 採用最新的 CSS-first 引擎進行響應式開發。
- **Axios**: 實作自定義攔截器處理認證隊列與無感續期。

### Backend
- **Spring Boot 3.4**: 核心開發框架，利用最新版本的性能優化。
- **Spring Security**: 結合 JWT 與 HttpOnly Cookie 實作安全過濾鏈。
- **Spring Data JPA (Hibernate)**: 處理訂單與用戶資料的持久化。
- **Spring Data Redis**: 利用 Lua Script 實作原子性庫存扣減。
- **RabbitMQ**: 實現非同步訂單處理，達成流量削峰，保護資料庫穩定。

---

## 📦 專案結構 (Project Structure)
- `/frontend`: 包含 Axios 認證攔截器與 UI 組件。
- `/backend`: 包含 Redis Lua 腳本、RabbitMQ 生產者/消費者、JWT 過濾器與 Restful API。
