-- wrapping 테이블 초기 데이터
INSERT INTO wrapping (
    wrapping_id,
    name,
    price,
    is_active
) VALUES
      (1, '기본 포장', 2000, TRUE),
      (2, '선물 포장', 5000, TRUE);

-- orders 테이블 초기 데이터
INSERT INTO orders (
    id,
    user_id,
    guest_name,
    guest_phone,
    status,
    orderdate_at,
    delivery_at,
    created_at,
    updated_at,
    total_price,
    delivery_fee,
    final_price
) VALUES
    (
        1,
        NULL,
        '테스트',
        '010-0000-0000',
        'PENDING',
        NOW(),
        NOW(),
        NOW(),
        NOW(),
        10000,
        3000,
        13000
    );