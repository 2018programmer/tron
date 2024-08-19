package com.dx.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GetBlockDTO implements Serializable {

    private List<ContactDTO> data;
}
