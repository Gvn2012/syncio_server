package io.github.gvn2012.search_service.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "posts")
public class PostIndex {
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String content;

    @Field(type = FieldType.Date)
    private LocalDateTime publishedAt;

    @Field(type = FieldType.Keyword)
    private String status;
}
