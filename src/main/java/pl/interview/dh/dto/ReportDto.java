package pl.interview.dh.dto;


import lombok.*;

import java.io.Serializable;

@Data
public class ReportDto implements Serializable {
    @Getter
    @Setter
    private String host;
    @Getter
    @Setter
    private String icmp_ping;
    @Getter
    @Setter
    private String tcp_ping;
    @Getter
    @Setter
    private String trace;
}
