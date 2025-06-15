package com.destridon.notio;

import com.destridon.notio.NotIO.Column;
import com.destridon.notio.NotIO.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SampleEntity extends Entity {

    @Column("Name")
    String name;


}
