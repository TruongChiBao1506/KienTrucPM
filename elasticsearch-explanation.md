# Giải thích về việc thêm Elasticsearch vào dự án

## Elasticsearch là gì?

Elasticsearch là một công cụ tìm kiếm và phân tích phân tán, mã nguồn mở, được xây dựng trên Apache Lucene. Nó được thiết kế để xử lý lượng lớn dữ liệu và cung cấp khả năng tìm kiếm full-text với hiệu suất cao, khả năng mở rộng và độ tin cậy.

## Công dụng của Elasticsearch trong dự án

Trong dự án review-service, Elasticsearch được thêm vào với các mục đích chính sau:

1. **Tìm kiếm full-text**: Cho phép tìm kiếm các đánh giá (reviews) dựa trên nội dung văn bản, không chỉ dựa trên các trường cụ thể.

2. **Tìm kiếm đa trường**: Có thể tìm kiếm đồng thời trên nhiều trường như nội dung đánh giá, tên người dùng, tên sản phẩm.

3. **Lọc và tìm kiếm nâng cao**: Hỗ trợ các tính năng lọc như tìm đánh giá theo rating, theo người dùng, theo sản phẩm.

4. **Hiệu suất cao**: Elasticsearch được tối ưu hóa cho việc tìm kiếm, giúp cải thiện hiệu suất so với việc chỉ sử dụng cơ sở dữ liệu quan hệ.

## Cách hoạt động của Elasticsearch trong dự án

### 1. Kiến trúc tổng quan

Dự án sử dụng mô hình lưu trữ kép (dual storage):
- **MariaDB**: Lưu trữ chính cho dữ liệu đánh giá
- **Elasticsearch**: Lưu trữ thứ cấp để phục vụ tìm kiếm nâng cao

### 2. Các thành phần chính

#### a. Cấu hình Elasticsearch (ElasticsearchConfig.java)
- Cấu hình kết nối đến Elasticsearch server
- Kích hoạt Spring Data Elasticsearch repositories

#### b. Mô hình dữ liệu Elasticsearch (ReviewDocument.java)
- Định nghĩa cấu trúc document trong Elasticsearch
- Sử dụng annotation `@Document` để ánh xạ với index "reviews"
- Các trường được đánh chỉ mục với các loại dữ liệu phù hợp (Text, Long, Integer, Date)
- Trường văn bản sử dụng analyzer "standard" để hỗ trợ tìm kiếm full-text

#### c. Repository Elasticsearch (ReviewElasticsearchRepository.java)
- Cung cấp các phương thức tìm kiếm đặc thù cho Elasticsearch
- Hỗ trợ tìm kiếm theo productId, userId, rating
- Hỗ trợ tìm kiếm full-text trong nội dung
- Cung cấp tìm kiếm đa trường (content, username, productName)

#### d. Mapper (ReviewMapper.java)
- Chuyển đổi giữa entity JPA (Review) và document Elasticsearch (ReviewDocument)
- Đảm bảo dữ liệu nhất quán giữa hai hệ thống lưu trữ

#### e. Service Layer (ReviewServiceImpl.java)
- Lưu đánh giá vào cả MariaDB và Elasticsearch
- Cung cấp các phương thức tìm kiếm sử dụng Elasticsearch
- Xử lý lỗi và cung cấp cơ chế fallback khi Elasticsearch không khả dụng

### 3. Luồng dữ liệu

1. **Khi tạo đánh giá mới**:
   - Đánh giá được lưu vào MariaDB
   - Đánh giá được chuyển đổi thành ReviewDocument và lưu vào Elasticsearch
   - Nếu việc lưu vào Elasticsearch thất bại, quá trình vẫn tiếp tục (không ảnh hưởng đến lưu trữ chính)

2. **Khi tìm kiếm đánh giá**:
   - Các tìm kiếm đơn giản (như theo productId) sử dụng MariaDB
   - Các tìm kiếm nâng cao (full-text, đa trường) sử dụng Elasticsearch
   - Nếu Elasticsearch không khả dụng, hệ thống fallback về MariaDB hoặc trả về kết quả trống

## Sau khi chạy ứng dụng

Sau khi chạy ứng dụng với Elasticsearch:

1. **Khởi động**:
   - Ứng dụng kết nối đến Elasticsearch server (mặc định là localhost:9200)
   - Nếu index "reviews" chưa tồn tại, nó sẽ được tạo tự động

2. **Hoạt động**:
   - Mỗi khi có đánh giá mới được tạo, nó sẽ được lưu vào cả MariaDB và Elasticsearch
   - Các API tìm kiếm có thể sử dụng khả năng tìm kiếm nâng cao của Elasticsearch

3. **Lợi ích**:
   - Tìm kiếm nhanh hơn và mạnh mẽ hơn
   - Khả năng tìm kiếm full-text trong nội dung đánh giá
   - Hỗ trợ tìm kiếm đa trường (nội dung, tên người dùng, tên sản phẩm)
   - Lọc đánh giá theo rating, người dùng, sản phẩm

4. **Xử lý lỗi**:
   - Nếu Elasticsearch không khả dụng, ứng dụng vẫn hoạt động với MariaDB
   - Các tính năng tìm kiếm nâng cao có thể bị hạn chế khi Elasticsearch không khả dụng

## Lưu ý

Hiện tại, các API tìm kiếm nâng cao của Elasticsearch chưa được hiển thị qua REST API (ReviewController). Các phương thức đã được triển khai ở service layer nhưng chưa có endpoint tương ứng. Để tận dụng đầy đủ khả năng của Elasticsearch, cần bổ sung các endpoint sau vào controller:

- Tìm kiếm đánh giá theo từ khóa (searchReviews)
- Lấy đánh giá theo userId (getReviewsByUserId)
- Lấy đánh giá theo rating cụ thể (getReviewsByRating)
- Lấy đánh giá theo rating tối thiểu (getReviewsByMinimumRating)