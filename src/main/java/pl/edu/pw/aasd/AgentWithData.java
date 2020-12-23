package pl.edu.pw.aasd;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AgentWithData<Data extends Jsonable> extends AgentWithUniqueName {

    protected Data data;

    protected void loadState() {
        try {
            var content = Files.readString(this.getDataFileName());

            this.data = this.parseData(content);
        } catch (Throwable ignore) {
            this.data = this.parseData(null);
        }
    }

    protected void saveState() {
        try {
            Files.write(this.getDataFileName(), data.toJson().toString().getBytes(StandardCharsets.UTF_8));
        } catch (Throwable ignore) {
        }
    }

    protected abstract Data parseData(String name);

    @Override
    protected void setup() {
        this.loadState();
    }

    @Override
    protected void takeDown() {
        this.saveState();
    }

    private Path getDataFileName() {
        return Path.of("data/" + this.getUniqueName() + ".json");
    }

    public Data getData() {
        return data;
    }

    protected void setData(Data data) {
        this.data = data;
    }
}
