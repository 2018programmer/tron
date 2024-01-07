package com.dx.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GetBlockDTO implements Serializable {

    private List<ContactDTO> data;
}
