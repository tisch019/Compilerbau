import java.io.IOException;
import java.util.*;

public class Main{
    public static void main(String[] args) {
        List<InterpreterError> errors = new LinkedList<>();
        try{
            Lexer lexer = new Lexer("input.fare");
            Filter filter = new Filter(lexer, errors);
            Parser parser = new Parser(filter, errors);
            CUNode root = parser.compilationUnit();
            root.semantischeAnalyse(null, errors);
            if(errors.size() == 0)
                root.run();
            else{
                System.out.println("Errors:");
                int i=1;
                for (CompilerError error:errors)
                    System.out.println(i++ +") "+error);
            }    
        }catch(IOException e){System.out.println(e);}
    }
}