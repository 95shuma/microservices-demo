INSERT INTO stock_items (sku, quantity, version) VALUES
    ('WIDGET-001', 100, 0),
    ('GADGET-002', 50, 0),
    ('GIZMO-003', 0, 0)
ON CONFLICT (sku) DO NOTHING;
