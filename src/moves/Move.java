package moves;

public class Move {
    private final String name;
    private final int damage;
    private final double splashRadius;
    private final double projectileSpeed;
    private final double gravityScale;

    public Move(String name, int damage, double splashRadius, double projectileSpeed, double gravityScale) {
        this.name = name;
        this.damage = damage;
        this.splashRadius = splashRadius;
        this.projectileSpeed = projectileSpeed;
        this.gravityScale = gravityScale;
    }

    public String getName() {
        return name;
    }

    public int getDamage() {
        return damage;
    }

    public double getSplashRadius() {
        return splashRadius;
    }

    public double getProjectileSpeed() {
        return projectileSpeed;
    }

    public double getGravityScale() {
        return gravityScale;
    }
}
