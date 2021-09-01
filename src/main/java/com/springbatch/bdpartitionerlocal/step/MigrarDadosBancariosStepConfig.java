package com.springbatch.bdpartitionerlocal.step;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import com.springbatch.bdpartitionerlocal.dominio.DadosBancarios;

@Configuration
public class MigrarDadosBancariosStepConfig {

	@Value("${migracaoDados.totalRegistros}")
	public Integer totalRegistros;

	@Value("${migracaoDados.gridSize}")
	public Integer gridSize;

	@Autowired
	private StepBuilderFactory stepBuilderFactory;

	@Autowired
	@Qualifier("transactionManagerApp")
	private PlatformTransactionManager transactionManagerApp;

	@Bean
	public Step migrarDadosBancariosManager(
			ItemReader<DadosBancarios> dadosBancariosReader,
			ItemWriter<DadosBancarios> dadosBancariosWriter,
			@Qualifier("dadosBancariosPartitioner") Partitioner partitioner, 
			TaskExecutor taskExecutor) {
		return stepBuilderFactory
				.get("migrarDadosBancariosManager")
				.partitioner("migrarDadosBancarios.manager", partitioner)
				.taskExecutor(taskExecutor).gridSize(gridSize)
				.step(migrarDadosBancariosStep(dadosBancariosReader, dadosBancariosWriter))
				.build();
	}

	private Step migrarDadosBancariosStep(
			ItemReader<DadosBancarios> dadosBancariosReader,
			ItemWriter<DadosBancarios> dadosBancariosWriter) {
		return stepBuilderFactory
				.get("migrarDadosBancariosStep")
				.<DadosBancarios, DadosBancarios>chunk(totalRegistros / gridSize)
				.reader(dadosBancariosReader)
				.writer(dadosBancariosWriter)
				.transactionManager(transactionManagerApp)
				.build();
	}
}
