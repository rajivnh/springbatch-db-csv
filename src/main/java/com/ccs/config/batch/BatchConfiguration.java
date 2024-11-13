package com.ccs.config.batch;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.RowMapper;

import com.ccs.model.EmpData;

@EnableBatchProcessing
@Configuration
public class BatchConfiguration {
	@Autowired
	StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	JobBuilderFactory jobBuilderFactory;
	
	@Qualifier("CcsDbDataSource")
	@Autowired
	DataSource dataSource;

	@Bean
	public JdbcCursorItemReader<EmpData> reader() {
		JdbcCursorItemReader<EmpData> reader = new JdbcCursorItemReader<EmpData>();
		
		reader.setDataSource(dataSource);
		reader.setSql("Select emp_number, emp_name, emp_join_dt, emp_addr from employee where rowNum < 10000");
		reader.setFetchSize(500);
		
		reader.setRowMapper(new RowMapper<EmpData>() {
			
			@Override
			public EmpData mapRow(ResultSet rs, int rowNum) throws SQLException {
				EmpData data = new EmpData();
				
				data.setEmpNumber(rs.getInt("emp_number"));
				data.setEmpName(rs.getString("emp_name"));
				data.setEmpJoinDt(rs.getDate("emp_join_dt"));
				data.setEmpAddr(rs.getString("emp_addr"));
				
				return data;
			}
		});
		
		return reader;
	}
	
	@Bean
	public FlatFileItemWriter<EmpData> writer() {
		FlatFileItemWriter<EmpData> writer = new FlatFileItemWriter<EmpData>();
		
		writer.setResource(new FileSystemResource("c:/aws/spring-batch/employee_out.csv"));
		writer.setAppendAllowed(true);
		
		writer.setHeaderCallback(new FlatFileHeaderCallback() {
			public void writeHeader(Writer writer) throws IOException {
                writer.write("EMP_NUMBER,EMP_NAME,EMP_JOIN_DT,EMP_ADDR");
            }
		});
		
		DelimitedLineAggregator<EmpData> aggregator = new DelimitedLineAggregator<EmpData>();

		BeanWrapperFieldExtractor<EmpData> fieldExtractor = new BeanWrapperFieldExtractor<EmpData>();
		fieldExtractor.setNames(new String[] {"empNumber", "empName", "empJoinDt", "empAddr"});
	
		aggregator.setFieldExtractor(fieldExtractor);
		writer.setLineAggregator(aggregator);

		return writer;
	}
	
	@Bean
	public Step executeStep() {
		return stepBuilderFactory.get("executeStep")
				.<EmpData, EmpData>chunk(200)
				.reader(reader())
				.processor(new ItemProcessor<EmpData, EmpData>() {
					public EmpData process(EmpData data) throws Exception {
						if(data.getEmpName() != null)
							data.setEmpName(data.getEmpName().toUpperCase());
						
						return data;
					}					
				})
				.writer(writer())
				.build();
	}
	
	@Bean
	public Job processJob() {
		return jobBuilderFactory.get("processJob")
				.incrementer(new RunIdIncrementer())
				.flow(executeStep())
				.end()
				.build();
	}
}
