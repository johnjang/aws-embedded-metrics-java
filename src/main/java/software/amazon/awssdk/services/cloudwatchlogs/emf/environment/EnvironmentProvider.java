package software.amazon.awssdk.services.cloudwatchlogs.emf.environment;

import software.amazon.awssdk.services.cloudwatchlogs.emf.config.Configuration;
import software.amazon.awssdk.services.cloudwatchlogs.emf.config.EnvironmentConfigurationProvider;

import java.util.Optional;

public class EnvironmentProvider {
    private final Configuration config = EnvironmentConfigurationProvider.getConfig();
    private final Environment lambdaEnvironment = new LambdaEnvironment();
    private final Environment defaultEnvironment = new DefaultEnvironment(config);
    private final Environment[] environments = new Environment[] {lambdaEnvironment, defaultEnvironment};

    private static Environment cachedEnvironment;


    //TODO: Support more environments
    public Environment resolveEnvironment() {
        if (cachedEnvironment != null) {
            return cachedEnvironment;
        }

        Optional<Environment> env = getEnvironmentFromOverride();

        cachedEnvironment = env.orElseGet(() -> discoverEnvironment().orElse(defaultEnvironment));
        return cachedEnvironment;
    }

    /**
     * A helper method to clean the cached environment in tests
     */
    void cleanResolvedEnvironment() {
        cachedEnvironment = null;
    }

    private Optional<Environment> discoverEnvironment() {
        for (Environment env: environments) {
            if (env.probe()) {
                return Optional.of(env);
            }
        }
        return Optional.empty();
    }

    private Optional<Environment> getEnvironmentFromOverride() {
        Configuration config = EnvironmentConfigurationProvider.getConfig();

        Optional<Environment> environment;
        switch (config.getEnvironmentOverride()) {
            case Lambda:
                environment = Optional.of(lambdaEnvironment);
                break;
            case Agent:
                environment = Optional.of(defaultEnvironment);
                break;
            default:
                environment = Optional.empty();
        }
        return environment;
    }
}