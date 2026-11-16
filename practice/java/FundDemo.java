import java.math.BigDecimal;

public class FundDemo {
    public static void main(String[] args) {
        // ===== 1. 成功场景测试 =====
        System.out.println("===== 成功场景 =====");
        Project p = new Project(new BigDecimal("1000.00"));
        FundApplication a = new FundApplication("传感器", new BigDecimal("600.00"));
        FundService svc = new FundService();

        System.out.println("初始      " + p.snapshot());

        svc.submit(p, a);
        System.out.println("提交      " + p.snapshot());

        svc.approve(p, a, new BigDecimal("500.00"));
        System.out.println("批准500   " + p.snapshot());

        svc.settle(p, a, new BigDecimal("460.00"));
        System.out.println("结算460   " + p.snapshot());

        // ===== 2. 失败场景测试 =====
        System.out.println("\n===== 失败场景（必须报错且金额不变） =====");
        
        Project p2 = new Project(new BigDecimal("1000.00"));
        FundService svc2 = new FundService();

        checkFail(p2, "余额不足", () -> svc2.submit(p2, new FundApplication("超支", new BigDecimal("9999.00"))));
        checkFail(p2, "三位小数", () -> svc2.submit(p2, new FundApplication("测试", new BigDecimal("1.234"))));
        
        // 测试重复结算
        FundApplication a2 = new FundApplication("测试", new BigDecimal("100.00"));
        svc2.submit(p2, a2);
        svc2.approve(p2, a2, new BigDecimal("100.00"));
        svc2.settle(p2, a2, new BigDecimal("100.00"));
        checkFail(p2, "已结算再结算", () -> svc2.settle(p2, a2, new BigDecimal("1.00")));

        // 【新增】学长要求的验证：已批准再提交
        FundApplication b = new FundApplication("测试", new BigDecimal("100.00"));
        svc2.submit(p2, b);
        svc2.approve(p2, b, new BigDecimal("100.00"));
        checkFail(p2, "已批准再提交", () -> svc2.submit(p2, b));
    }

    // 验收小工具
    static void checkFail(Project p, String name, Runnable action) {
        String before = p.snapshot();
        try {
            action.run();
            System.out.println("❌ " + name + " 竟然没报错！");
        } catch (RuntimeException e) {
            boolean unchanged = before.equals(p.snapshot());
            System.out.println("✅ " + name + " -> " + e.getMessage() + (unchanged ? " (金额未变)" : " (❌ 金额被改了！)"));
        }
    }
}