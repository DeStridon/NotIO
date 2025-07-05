package com.destridon.notio;

import java.util.List;
import com.destridon.notio.config.EnvConfig;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class ExistingDbTest {

	@Test
	public void test1() {
		String apiKey = EnvConfig.getEnv("NOTION_API_KEY");
		String databaseId = EnvConfig.getEnv("NOTION_DATABASE_ID");
		
		NotionDatabase notionDatabase = NotIO.database(apiKey, databaseId);

		SampleEntity sampleEntity = SampleEntity.builder().name("Test").build();

		notionDatabase.updateOrInsert(sampleEntity);
		
		List<SampleEntity> sampleEntities = notionDatabase.getEntries(SampleEntity.class);

		Assert.assertTrue(sampleEntities.stream().anyMatch(x -> x.getName().equals("Test")));
	}
}
