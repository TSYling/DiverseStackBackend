package top.richlin.security.form;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * SearchDetailForm
 *
 * @author wsl
 * @version 1.0
 * @date 2023/7/29 17:41
 * @description
 */
@AllArgsConstructor
@Data
public class SearchDetailForm {
    private String key;
    private String pn;
    private String rn;
}
