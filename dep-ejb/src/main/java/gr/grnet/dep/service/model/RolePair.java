package gr.grnet.dep.service.model;


public class RolePair {

    Role.RoleDiscriminator first;

    Role.RoleDiscriminator second;

    public RolePair(Role.RoleDiscriminator first, Role.RoleDiscriminator second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((first == null) ? 0 : first.hashCode());
        result = prime * result + ((second == null) ? 0 : second.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        RolePair other = (RolePair) obj;
        if (!first.equals(other.first)) {
            return false;
        }
        if (!second.equals(other.second)) {
            return false;
        }
        return true;
    }
}
