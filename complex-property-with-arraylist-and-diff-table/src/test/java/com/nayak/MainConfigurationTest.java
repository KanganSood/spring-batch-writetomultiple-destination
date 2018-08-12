package com.nayak;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.test.MetaDataInstanceFactory;
import org.springframework.batch.test.StepScopeTestExecutionListener;
import org.springframework.batch.test.StepScopeTestUtils;
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
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class})
@Transactional
public class MainConfigurationTest {
    @Autowired
    private Job job;

    private JobParameters jobParameters;

    @Autowired
    AddressItemWriter addressItemWriter;

    @Autowired
    ItemWriter<PersonData> personItemWriter;

    JdbcOperations jdbcOperations;

    @Autowired
    DataSource dataSource;


    @Before
    public void setUp() {
        Map<String, JobParameter> params = new HashMap<>();
        jobParameters = new JobParameters(params);
        this.jdbcOperations=new JdbcTemplate(dataSource);

    }

    @After
    public void cleanupPostTest(){
        jdbcOperations.update("DELETE FROM PERSON");
    }

    @Test
    public void checkJob() {
        assertNotNull(job);
        assertEquals("job1", job.getName());
    }

    @Test
    public void personItemWriterTest()  {
        PersonData person1=PersonData.builder().firstName("sushil1").build();
        PersonData person2=PersonData.builder().firstName("sushil2").build();
        PersonData person3=PersonData.builder().firstName("sushil3").build();

        StepExecution execution = MetaDataInstanceFactory.createStepExecution();
        try {
            StepScopeTestUtils.doInStepScope(execution, () -> {
                personItemWriter.write(Arrays.asList(person1,person2,person3));
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        List<PersonData> personDataList=jdbcOperations.query("select * from PERSON",
            (rs, i) -> PersonData.builder().firstName(rs.getString("first-name")).build());

        assertThat(personDataList.size(), Matchers.equalTo(3));

        assertArrayEquals( personDataList.stream().map(PersonData::getFirstName).toArray(), new String[] {"sushil1","sushil2","sushil3"});
    }

    @Test
    public void personCompositeItemWriterTest(){

    }



}
