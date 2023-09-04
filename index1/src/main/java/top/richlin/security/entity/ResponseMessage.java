package top.richlin.security.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * ResponseMessage
 *
 * @author wsl
 * @version 1.0
 * @date 2023/9/2 20:26
 * @description
 */
@AllArgsConstructor
@Data
public class ResponseMessage {
    private int code;
    private Map<String,Object> contextMap;

}
