package com.nayak;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
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
@ActiveProfiles("dev")
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, StepScopeTestExecutionListener.class})
@Transactional
public class AddressItemWriterTest {

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

//    @After
//    public void cleanupPostTest(){
//        this.jdbcOperations.update("DELETE FROM ADDRESS");
//    }

    @Test
    public void testAddressWriterWithSingleAddressForOnePersonOnly() throws Exception {
        Address add1=Address.builder().passportNumber("xxxx").streetNumber("1").street("street 1").postalCode("post-code 1").city("what 1").apartmentNumber("21").build();

        PersonData personData=PersonData.builder().address(Arrays.asList(add1)).build();

        StepExecution execution = MetaDataInstanceFactory.createStepExecution();
        StepScopeTestUtils.doInStepScope(execution, () -> {
            addressItemWriter.write(Arrays.asList(personData));
            return null;
        });

        Address address=jdbcOperations.queryForObject("select * from ADDRESS",
            (rs, i) -> Address.builder()
                .city(rs.getString("city"))
                .postalCode("post-code 1")
                .street("street 1")
                .streetNumber("1")
                .passportNumber("xxxx")
                .build());

        assertThat(jdbcOperations.queryForObject("select count(*) from ADDRESS",Integer.class)).isEqualTo(1);
        assertThat(address.getCity()).isEqualTo("what 1");
        assertThat(address.getPostalCode()).isEqualTo("post-code 1");
        assertThat(address.getPassportNumber()).isEqualTo("xxxx");

    }


    @Test
    public void testAddressWriterWithMultipleAddressForOnePersonOnly() throws Exception {
        Address add1=Address.builder().passportNumber("xxxx").streetNumber("1").street("street 1").postalCode("post-code 1").city("what 1").apartmentNumber("21").build();
        Address add2=Address.builder().passportNumber("xxxx").streetNumber("2").street("street 2").postalCode("post-code 2").city("what 1").apartmentNumber("22").build();
        Address add3=Address.builder().passportNumber("xxxx").streetNumber("3").street("street 3").postalCode("post-code 3").city("what 2").apartmentNumber("23").build();

        PersonData personData=PersonData.builder().address(Arrays.asList(add1,add3,add2)).build();

        StepExecution execution = MetaDataInstanceFactory.createStepExecution();
        StepScopeTestUtils.doInStepScope(execution, () -> {
            addressItemWriter.write(Arrays.asList(personData));
            return null;
        });

        List<Address> address=jdbcOperations.query("select * from ADDRESS",
            (rs, i) -> Address.builder()
                .city(rs.getString("city"))
                .postalCode("post-code")
                .street("street 1")
                .streetNumber("1")
                .passportNumber("xxxx")
                .build());

        assertThat(address.size()).isEqualTo(3);
    }
}
