package wodss.timecastbackend.util;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.codehaus.jackson.JsonProcessingException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;

public class DateSerializer extends StdSerializer<LocalDate> {

    private SimpleDateFormat formatter
            = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");

    public DateSerializer() {
        this(null);
    }

    public DateSerializer(Class t) {
        super(t);
    }

    @Override
    public void serialize (LocalDate value, JsonGenerator gen, SerializerProvider arg2)
            throws IOException, JsonProcessingException {
        gen.writeString(formatter.format(value));
    }
}
