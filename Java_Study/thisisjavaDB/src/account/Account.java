package account;

import lombok.*;

// DTO

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Account {
    private String ano;
    private String owner;
    private int balance;

}
