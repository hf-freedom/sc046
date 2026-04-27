package com.training.service;

import com.training.model.*;
import com.training.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private ExamRepository examRepository;

    @Override
    public void run(String... args) {
        if (departmentRepository.findAll().isEmpty()) {
            initDepartments();
        }
        if (employeeRepository.findAll().isEmpty()) {
            initEmployees();
        }
        if (courseRepository.findAll().isEmpty()) {
            initCourses();
            initChapters();
            initExams();
        }
    }

    private void initDepartments() {
        Department dept1 = Department.builder()
                .name("技术研发部")
                .code("TECH")
                .annualBudget(new BigDecimal("100000.00"))
                .usedBudget(new BigDecimal("0.00"))
                .reservedBudget(new BigDecimal("0.00"))
                .build();
        departmentRepository.save(dept1);

        Department dept2 = Department.builder()
                .name("市场营销部")
                .code("MARKET")
                .annualBudget(new BigDecimal("50000.00"))
                .usedBudget(new BigDecimal("0.00"))
                .reservedBudget(new BigDecimal("0.00"))
                .build();
        departmentRepository.save(dept2);

        Department dept3 = Department.builder()
                .name("人力资源部")
                .code("HR")
                .annualBudget(new BigDecimal("30000.00"))
                .usedBudget(new BigDecimal("0.00"))
                .reservedBudget(new BigDecimal("0.00"))
                .build();
        departmentRepository.save(dept3);
    }

    private void initEmployees() {
        Employee emp1 = Employee.builder()
                .name("张三")
                .employeeNo("EMP001")
                .departmentId(1L)
                .position("高级工程师")
                .email("zhangsan@company.com")
                .phone("13800138001")
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();
        employeeRepository.save(emp1);

        Employee emp2 = Employee.builder()
                .name("李四")
                .employeeNo("EMP002")
                .departmentId(1L)
                .position("中级工程师")
                .email("lisi@company.com")
                .phone("13800138002")
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();
        employeeRepository.save(emp2);

        Employee emp3 = Employee.builder()
                .name("王五")
                .employeeNo("EMP003")
                .departmentId(2L)
                .position("市场经理")
                .email("wangwu@company.com")
                .phone("13800138003")
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();
        employeeRepository.save(emp3);

        Employee emp4 = Employee.builder()
                .name("赵六")
                .employeeNo("EMP004")
                .departmentId(3L)
                .position("HR专员")
                .email("zhaoliu@company.com")
                .phone("13800138004")
                .status(Employee.EmployeeStatus.ACTIVE)
                .build();
        employeeRepository.save(emp4);
    }

    private void initCourses() {
        Course course1 = Course.builder()
                .name("Java 8 新特性深入学习")
                .code("JAVA001")
                .description("深入学习Java 8的Lambda表达式、Stream API、Optional等新特性")
                .courseFee(new BigDecimal("5000.00"))
                .maxParticipants(30)
                .currentParticipants(0)
                .totalChapters(5)
                .status(Course.CourseStatus.PUBLISHED)
                .build();
        courseRepository.save(course1);

        Course course2 = Course.builder()
                .name("Spring Boot 实战开发")
                .code("SB001")
                .description("从入门到精通，掌握Spring Boot框架的核心技术")
                .courseFee(new BigDecimal("8000.00"))
                .maxParticipants(20)
                .currentParticipants(0)
                .totalChapters(6)
                .status(Course.CourseStatus.PUBLISHED)
                .build();
        courseRepository.save(course2);

        Course course3 = Course.builder()
                .name("市场营销策略与实践")
                .code("MARKET001")
                .description("学习现代市场营销策略，提升市场分析和推广能力")
                .courseFee(new BigDecimal("3000.00"))
                .maxParticipants(25)
                .currentParticipants(0)
                .totalChapters(4)
                .status(Course.CourseStatus.PUBLISHED)
                .build();
        courseRepository.save(course3);
    }

    private void initChapters() {
        Chapter chapter1 = Chapter.builder()
                .courseId(1L)
                .title("Lambda表达式基础")
                .content("学习Lambda表达式的语法和使用场景")
                .chapterOrder(1)
                .minStudyDurationMinutes(60)
                .isMandatory(true)
                .prerequisiteChapterIds(Collections.emptyList())
                .build();
        chapterRepository.save(chapter1);

        Chapter chapter2 = Chapter.builder()
                .courseId(1L)
                .title("函数式接口")
                .content("深入理解函数式接口的设计与应用")
                .chapterOrder(2)
                .minStudyDurationMinutes(45)
                .isMandatory(true)
                .prerequisiteChapterIds(Arrays.asList(1L))
                .build();
        chapterRepository.save(chapter2);

        Chapter chapter3 = Chapter.builder()
                .courseId(1L)
                .title("Stream API入门")
                .content("学习Stream API的基本操作")
                .chapterOrder(3)
                .minStudyDurationMinutes(90)
                .isMandatory(true)
                .prerequisiteChapterIds(Arrays.asList(1L, 2L))
                .build();
        chapterRepository.save(chapter3);

        Chapter chapter4 = Chapter.builder()
                .courseId(1L)
                .title("Stream API高级操作")
                .content("深入学习Stream API的高级特性")
                .chapterOrder(4)
                .minStudyDurationMinutes(120)
                .isMandatory(true)
                .prerequisiteChapterIds(Arrays.asList(3L))
                .build();
        chapterRepository.save(chapter4);

        Chapter chapter5 = Chapter.builder()
                .courseId(1L)
                .title("Optional与新日期API")
                .content("学习Optional类和Java 8新日期API")
                .chapterOrder(5)
                .minStudyDurationMinutes(60)
                .isMandatory(false)
                .prerequisiteChapterIds(Collections.emptyList())
                .build();
        chapterRepository.save(chapter5);

        for (int i = 1; i <= 6; i++) {
            Chapter chapter = Chapter.builder()
                    .courseId(2L)
                    .title("Spring Boot 第" + i + "章")
                    .content("Spring Boot核心内容第" + i + "章")
                    .chapterOrder(i)
                    .minStudyDurationMinutes(90)
                    .isMandatory(i <= 4)
                    .prerequisiteChapterIds(i > 1 ? Arrays.asList((long) (i - 1)) : Collections.emptyList())
                    .build();
            chapterRepository.save(chapter);
        }

        for (int i = 1; i <= 4; i++) {
            Chapter chapter = Chapter.builder()
                    .courseId(3L)
                    .title("市场营销第" + i + "章")
                    .content("市场营销核心内容第" + i + "章")
                    .chapterOrder(i)
                    .minStudyDurationMinutes(60)
                    .isMandatory(true)
                    .prerequisiteChapterIds(i > 1 ? Arrays.asList((long) (i - 1)) : Collections.emptyList())
                    .build();
            chapterRepository.save(chapter);
        }
    }

    private void initExams() {
        Exam exam1 = Exam.builder()
                .courseId(1L)
                .name("Java 8 新特性考试")
                .totalQuestions(10)
                .passingScore(70)
                .durationMinutes(60)
                .maxRetakeAttempts(2)
                .questions(Arrays.asList(
                        createQuestion(1L, "Lambda表达式的箭头符号是？",
                                Arrays.asList("->", "=>", "->>", ">>"), 0, 10),
                        createQuestion(2L, "以下哪个是函数式接口？",
                                Arrays.asList("List", "Runnable", "Map", "Set"), 1, 10),
                        createQuestion(3L, "Stream API的中间操作返回什么？",
                                Arrays.asList("void", "Stream", "List", "Optional"), 1, 10),
                        createQuestion(4L, "Optional类的主要用途是？",
                                Arrays.asList("存储数据", "避免空指针异常", "类型转换", "性能优化"), 1, 10),
                        createQuestion(5L, "Java 8新日期API的核心包是？",
                                Arrays.asList("java.util", "java.time", "java.sql", "java.text"), 1, 10),
                        createQuestion(6L, "以下哪个是Stream的终止操作？",
                                Arrays.asList("map", "filter", "collect", "sorted"), 2, 10),
                        createQuestion(7L, "Predicate接口的返回类型是？",
                                Arrays.asList("void", "boolean", "Object", "int"), 1, 10),
                        createQuestion(8L, "Consumer接口的抽象方法是？",
                                Arrays.asList("apply", "accept", "test", "get"), 1, 10),
                        createQuestion(9L, "Function接口的输入和输出？",
                                Arrays.asList("相同类型", "不同类型", "只能是String", "只能是数字"), 1, 10),
                        createQuestion(10L, "Supplier接口的特点是？",
                                Arrays.asList("有输入无输出", "无输入有输出", "有输入有输出", "无输入无输出"), 1, 10)
                ))
                .build();
        examRepository.save(exam1);

        Exam exam2 = Exam.builder()
                .courseId(2L)
                .name("Spring Boot 考试")
                .totalQuestions(10)
                .passingScore(70)
                .durationMinutes(90)
                .maxRetakeAttempts(2)
                .questions(Arrays.asList(
                        createQuestion(11L, "Spring Boot的核心注解是？",
                                Arrays.asList("@Component", "@SpringBootApplication", "@Configuration", "@Bean"), 1, 10),
                        createQuestion(12L, "Spring Boot默认使用的服务器是？",
                                Arrays.asList("Tomcat", "Jetty", "Undertow", "WebLogic"), 0, 10),
                        createQuestion(13L, "Spring Boot的配置文件优先级？",
                                Arrays.asList("application.yml > application.properties",
                                        "application.properties > application.yml",
                                        "相同优先级", "无法比较"), 0, 10),
                        createQuestion(14L, "@RestController注解包含？",
                                Arrays.asList("@Controller", "@ResponseBody", "@Controller和@ResponseBody", "都不是"), 2, 10),
                        createQuestion(15L, "Spring Boot的自动配置原理？",
                                Arrays.asList("@EnableAutoConfiguration", "@ComponentScan", "@Configuration", "@Bean"), 0, 10),
                        createQuestion(16L, "以下哪个是Spring Boot Starter？",
                                Arrays.asList("spring-boot-starter-web", "spring-core", "spring-context", "spring-beans"), 0, 10),
                        createQuestion(17L, "Spring Boot的热部署工具？",
                                Arrays.asList("spring-boot-devtools", "spring-boot-starter-test",
                                        "spring-boot-starter-actuator", "spring-boot-starter-security"), 0, 10),
                        createQuestion(18L, "Spring Boot Actuator用于？",
                                Arrays.asList("监控应用", "安全认证", "数据访问", "日志记录"), 0, 10),
                        createQuestion(19L, "Spring Boot的默认端口？",
                                Arrays.asList("8080", "8081", "9090", "8000"), 0, 10),
                        createQuestion(20L, "Spring Boot如何排除自动配置？",
                                Arrays.asList("exclude属性", "exclude属性", "exclude属性", "都不是"), 0, 10)
                ))
                .build();
        examRepository.save(exam2);

        Exam exam3 = Exam.builder()
                .courseId(3L)
                .name("市场营销考试")
                .totalQuestions(10)
                .passingScore(60)
                .durationMinutes(60)
                .maxRetakeAttempts(3)
                .questions(Arrays.asList(
                        createQuestion(21L, "市场营销的核心是？",
                                Arrays.asList("销售产品", "满足客户需求", "广告宣传", "降低价格"), 1, 10),
                        createQuestion(22L, "4P理论不包括？",
                                Arrays.asList("产品Product", "价格Price", "渠道Place", "人员People"), 3, 10),
                        createQuestion(23L, "目标市场定位的目的是？",
                                Arrays.asList("扩大市场份额", "找到合适的客户群体", "降低成本", "提高品牌知名度"), 1, 10),
                        createQuestion(24L, "品牌建设的核心是？",
                                Arrays.asList("广告投放", "产品质量", "客户认知", "价格策略"), 2, 10),
                        createQuestion(25L, "市场调研的主要方法？",
                                Arrays.asList("问卷调查", "数据分析", "用户访谈", "以上都是"), 3, 10),
                        createQuestion(26L, "竞争对手分析的目的？",
                                Arrays.asList("抄袭对手", "了解优劣势", "打击对手", "无关紧要"), 1, 10),
                        createQuestion(27L, "客户关系管理(CRM)的核心？",
                                Arrays.asList("销售数据", "客户满意度", "市场份额", "产品质量"), 1, 10),
                        createQuestion(28L, "数字营销包括？",
                                Arrays.asList("社交媒体营销", "搜索引擎营销", "内容营销", "以上都是"), 3, 10),
                        createQuestion(29L, "营销预算的分配原则？",
                                Arrays.asList("平均分配", "按效果分配", "按历史分配", "按领导指示"), 1, 10),
                        createQuestion(30L, "营销效果评估的指标？",
                                Arrays.asList("销售额", "客户转化率", "ROI", "以上都是"), 3, 10)
                ))
                .build();
        examRepository.save(exam3);
    }

    private ExamQuestion createQuestion(Long id, String text,
                                        java.util.List<String> options,
                                        int correctIndex, int points) {
        return ExamQuestion.builder()
                .id(id)
                .questionText(text)
                .options(options)
                .correctAnswerIndex(correctIndex)
                .points(points)
                .build();
    }
}
