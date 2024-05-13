package com.integration.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.InboundChannelAdapter;
import org.springframework.integration.annotation.Poller;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;

@Configuration
public class IntegrationConfig {

	@Autowired
	private FileTransformer fileTransformer;
	
	@Bean
    public IntegrationFlow integrationFlow() {
		return IntegrationFlows.from(fileReader(),
                spec -> spec.poller(Pollers.fixedDelay(1000)))
                .transform(fileTransformer, "transForm")
                .handle(fileWriter())
                .get();
		
	}
	
	 @Bean
	 @InboundChannelAdapter(value="fileInputChannel", poller = @Poller(fixedDelay = "1000"))
	    public FileReadingMessageSource fileReader() {
		 	CompositeFileListFilter<File> filter = new CompositeFileListFilter<>();
		 	filter.addFilter(new SimplePatternFileListFilter(".txt"));
	        FileReadingMessageSource source = new FileReadingMessageSource();
	        source.setDirectory(new File("source"));
	        source.setFilter(filter);
	        return source;
	    }
	 @Bean
	 @ServiceActivator(inputChannel="fileInputChannel")
	    public FileWritingMessageHandler fileWriter() {
	        FileWritingMessageHandler handler = new FileWritingMessageHandler(
	                new File("destination")
	        );
	        handler.setAutoCreateDirectory(true);
	        handler.setExpectReply(false);
	        return handler;
	    }
}
