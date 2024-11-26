package dev.totallyspies.spydle.frontend.use_cases.update_string;


import lombok.Data;

@Data
public class UpdateStringInputData {
    private String guess;

    public UpdateStringInputData(String guess){
        this.guess = guess;
    }
}
