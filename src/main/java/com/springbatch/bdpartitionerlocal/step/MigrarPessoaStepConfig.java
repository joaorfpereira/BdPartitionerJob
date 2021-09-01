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

import com.springbatch.bdpartitionerlocal.dominio.Pessoa;

@Configuration
public class MigrarPessoaStepConfig {

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
	public Step migrarPessoaManager(
			ItemReader<Pessoa> pessoaReader, 
			ItemWriter<Pessoa> pessoaWriter,
			@Qualifier("pessoaPartitioner") Partitioner partitioner,
			TaskExecutor taskExecutor) {
		return stepBuilderFactory
				.get("migrarPessoaManager")
				.partitioner("migrarPessoa.manager", partitioner)
				.taskExecutor(taskExecutor)
				.gridSize(gridSize)
				.step(migrarPessoaStep(pessoaReader, pessoaWriter))
				.build();
	}

	private Step migrarPessoaStep(
			ItemReader<Pessoa> pessoaReader,
			ItemWriter<Pessoa> pessoaWriter) {
		return stepBuilderFactory
				.get("migrarPessoaStep")
				.<Pessoa, Pessoa>chunk(totalRegistros/gridSize)
				.reader(pessoaReader)
				.writer(pessoaWriter)
				.transactionManager(transactionManagerApp)
				.build();
	}
}
