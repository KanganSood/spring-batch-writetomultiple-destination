package com.nayak;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.codearte.jfairy.Fairy;
import io.codearte.jfairy.producer.person.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@SpringBootApplication
@EnableBatchProcessing
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
class
PersonData {
	private Address address;
	private String firstName;
	private String middleName;
	private String lastName;
	private String email;
	private String passportNumber;
}

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
class Address {
	private String postalCode;
	private String city;
	private String street;
	private String streetNumber;
	private String apartmentNumber;

	private String passportNumber;
}

class CustomItemReader implements ItemReader<PersonData> {

	private final Iterator<PersonData> data;

	public CustomItemReader(List<PersonData> personData) {
		this.data = personData.iterator();
	}

	@Override
	public PersonData read() throws Exception, UnexpectedInputException, ParseException,
			NonTransientResourceException {
		if (this.data.hasNext()) {
			return this.data.next();
		}
		else {
			return null;
		}
	}
}

@Configuration
class MainConfiguration {

	@Autowired
	StepBuilderFactory stepBuilderFactory;
	@Autowired
	JobBuilderFactory jobBuilderFactory;

	@Autowired
	DataSource dataSource;

	Fairy fairy;

	public MainConfiguration() {
		fairy = Fairy.create();
	}

	@Bean
	public CustomItemReader customItemReader() {
		List<PersonData> personList = new ArrayList<>();

		for (int i = 0; i < 10000; i++) {

			Person person = fairy.person();
			Address address = Address.builder()
					.apartmentNumber(person.getAddress().apartmentNumber())
					.city(person.getAddress().getCity())
					.postalCode(person.getAddress().getPostalCode())
					.street(person.getAddress().street())
					.streetNumber(person.getAddress().streetNumber())
					.passportNumber(person.passportNumber()).build();

			PersonData personData = PersonData.builder().firstName(person.firstName())
					.middleName(person.middleName()).lastName(person.lastName())
					.address(address).email(person.email())
					.passportNumber(person.passportNumber()).build();

			personList.add(personData);
		}
		return new CustomItemReader(personList);
	}

	@Bean
	public ItemWriter<PersonData> personItemWriter() {
		JdbcBatchItemWriter<PersonData> personJdbcBatchItemWriter = new JdbcBatchItemWriter<>();
		personJdbcBatchItemWriter.setSql(
				"INSERT INTO PERSON  VALUES(:firstName, :middleName, :lastName , :email, :passportNumber)");
		personJdbcBatchItemWriter.setDataSource(dataSource);
		personJdbcBatchItemWriter.setItemSqlParameterSourceProvider(
				new BeanPropertyItemSqlParameterSourceProvider<>());
		personJdbcBatchItemWriter.afterPropertiesSet();
		return personJdbcBatchItemWriter;
	}

	@Bean
	ItemWriter<PersonData> addressItemWriter() {
		JdbcBatchItemWriter<PersonData> addressJdbcBatchItemWriter = new JdbcBatchItemWriter<>();
		addressJdbcBatchItemWriter.setDataSource(dataSource);

		addressJdbcBatchItemWriter
				.setItemSqlParameterSourceProvider(person -> new MapSqlParameterSource()
						.addValue("postalCode", person.getAddress().getPostalCode())
						.addValue("city", person.getAddress().getCity())
						.addValue("street", person.getAddress().getStreet())
						.addValue("streetNumber", person.getAddress().getStreetNumber())
						.addValue("apartmentNumber",
								person.getAddress().getApartmentNumber(), Types.VARCHAR)
						.addValue("passportNumber", person.getPassportNumber(),
								Types.VARCHAR));
		addressJdbcBatchItemWriter.setSql(
				"INSERT INTO ADDRESS VALUES(:postalCode, :city, :street, :streetNumber, :apartmentNumber, :passportNumber)");

		addressJdbcBatchItemWriter.afterPropertiesSet();

		return addressJdbcBatchItemWriter;
	}

	@Bean
	public CompositeItemWriter<PersonData> personCompositeItemWriter() throws Exception {
		List<ItemWriter<? super PersonData>> itemWriterList = new ArrayList<>();
		itemWriterList.add(personItemWriter());
		itemWriterList.add(addressItemWriter());

		CompositeItemWriter<PersonData> itemWriter = new CompositeItemWriter<>();
		itemWriter.setDelegates(itemWriterList);

		itemWriter.afterPropertiesSet();
		return itemWriter;
	}

	@Bean
	public Step step1() throws Exception {
		return stepBuilderFactory.get("step1").<PersonData, PersonData> chunk(10)
				.reader(customItemReader()).writer(personCompositeItemWriter()).build();
	}

	@Bean
	public Job job1a() throws Exception {
		return jobBuilderFactory.get("job1").start(step1()).build();
	}
}

@RestController
class MainController {
	@Autowired
	JobLauncher jobLauncher;
	@Autowired
	Job job;

	@PostMapping("/")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void launch(@RequestParam("name") String name) throws Exception{
        JobParameters jobParameters=new JobParametersBuilder().addString("name",name).toJobParameters();

        this.jobLauncher.run( job, jobParameters);
    }

}
