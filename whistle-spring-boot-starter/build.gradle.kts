val springBootVersion = "3.5.10"
val assertjVersion = "3.27.6"

dependencies {
    api(project(":whistle-slack"))
    /*
     * 런타임은 사용처의 Spring Boot 버전 사용
     */
    compileOnly("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")

    testImplementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    testImplementation("org.springframework.boot:spring-boot-test:$springBootVersion")
    /*
     * AssertableApplicationContext 가 AssertJ 타입을 상속해 컴파일에 필요
     */
    testImplementation("org.assertj:assertj-core:$assertjVersion")
}
