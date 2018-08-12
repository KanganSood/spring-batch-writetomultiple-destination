package com.nayak;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("dev")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class})
@Transactional
public class CustomItemReaderTest {
    @Autowired
    private Job job;

    private JobParameters jobParameters;

    @Autowired
    AddressItemWriter addressItemWriter;

    JdbcOperations jdbcOperations;

    @Autowired
    DataSource dataSource;


    @Before
    public void setUp() {
        Map<String, JobParameter> params = new HashMap<>();
        jobParameters = new JobParameters(params);
        this.jdbcOperations=new JdbcTemplate(dataSource);

    }

    @Test
    public void readerTest(){

    }
}
