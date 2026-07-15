# MapStruct DTO 轉換重構 任務表
**建立日期**: 2026-07-15
**重構目標**: MemberController / EventController / ProductController / OrderController 的手動 Entity→DTO 轉換方法改用 MapStruct
**狀態**: ✅ 完成

---

## 重構清單

| # | 檔案:行號 | Smell 類型 | 重構動作 |
|---|---------|-----------|---------|
| 1 | pom.xml | 缺少依賴 | 新增 mapstruct / mapstruct-processor / lombok-mapstruct-binding，設定 maven-compiler-plugin annotationProcessorPaths |
| 2 | MemberController.java:112-120 | 手動轉換 | 新增 `mapper/MemberMapper.java`，Controller 改注入使用 |
| 3 | EventController.java:107-118 | 手動轉換 | 新增 `mapper/EventMapper.java`，Controller 改注入使用 |
| 4 | ProductController.java:138-155 | 手動轉換 | 新增 `mapper/ProductMapper.java`，Controller 改注入使用 |
| 5 | OrderController.java:154-193 / OrderServiceImpl.java:259-268 | 手動轉換 + Duplicate Code | 新增 `mapper/OrderMapper.java`；OrderServiceImpl 內部改呼叫 mapper；OrderController 刪除私有 `convertToClientResponse`，改呼叫 `orderService.convertToClientResponse()`；`convertToClientDetailResponse`/`convertToAdminResponse` 改直接呼叫注入的 `OrderMapper` |

## 驗收條件

- [x] `./mvnw clean package -DskipTests` 編譯成功（含 MapStruct annotation processing）
- [x] `./mvnw test` 相關測試通過（全部 11 個測試，含 BackendApplicationTests context load）
- [x] 所有 API 回傳的 JSON 欄位與重構前完全一致（含 null-safe fallback "Unknown" 邏輯）— 已比對 MapStruct 產生的 Impl 原始碼確認邏輯等價

---

## 開發紀錄

> 每完成一個重構項目，立即填寫一筆紀錄。

| 步驟 | 時間 | 動作說明 | 結果 |
|------|------|---------|------|
| 1 | 2026-07-15 | pom.xml 新增 mapstruct / mapstruct-processor / lombok-mapstruct-binding 依賴，設定 maven-compiler-plugin annotationProcessorPaths（順序：lombok → mapstruct-processor → lombok-mapstruct-binding） | 成功，編譯通過 |
| 2 | 2026-07-15 | 新增 `mapper/MemberMapper.java`（Member→MemberResponse） | 成功 |
| 3 | 2026-07-15 | 新增 `mapper/EventMapper.java`（Event→EventResponse） | 成功 |
| 4 | 2026-07-15 | 新增 `mapper/ProductMapper.java`（Product→ProductClientResponse/ProductAdminResponse） | 成功 |
| 5 | 2026-07-15 | 新增 `mapper/OrderMapper.java`（Order→OrderClientDetailResponse 兩種來源 + OrderAdminResponse，巢狀 Member/Product null-safe 用 `@Mapping(expression=...)`） | 成功 |
| 6 | 2026-07-15 | MemberController/EventController/ProductController 移除手動 convertToXxx，改注入對應 Mapper | 成功，編譯通過 |
| 7 | 2026-07-15 | OrderController 移除 3 個手動轉換方法，改注入 OrderMapper；其中 convertToClientResponse 改呼叫 `orderService.convertToClientResponse()` | 成功 |
| 8 | 2026-07-15 | OrderServiceImpl 注入 OrderMapper，convertToClientResponse 內部改委派給 mapper（消除與 OrderController 的重複邏輯） | 成功 |
| 9 | 2026-07-15 | OrderServiceTest 補上 `@Mock OrderMapper` 以配合新增的建構子相依 | 成功，10 個測試全過 |
| 10 | 2026-07-15 | 執行 `./mvnw test`（全部）與 `./mvnw clean package -DskipTests` 驗證 | BUILD SUCCESS，11 個測試全過 |
