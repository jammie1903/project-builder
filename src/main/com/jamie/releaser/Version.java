package com.jamie.releaser;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

public class Version implements Comparable<Version>{

    public static enum VersionSegment {
        MAJOR, MINOR, PATCH;
    }

    private int major;
    private int minor;
    private int patch;

    public Version(String rawVersion) {
        var matcher = Pattern.compile("v?(\\d+)\\.(\\d+)\\.(\\d+)").matcher(rawVersion);
        if (!matcher.find()) {
            throw new InvalidParameterException("invalid version?");
        }

        this.major = Integer.parseInt(matcher.group(1));
        this.minor = Integer.parseInt(matcher.group(2));
        this.patch = Integer.parseInt(matcher.group(3));
    }

    public Version(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public void increment(VersionSegment incrementType) {
        switch (incrementType) {
            case MAJOR:
                this.minor = 0;
                this.patch = 0;
                this.major++;
                break;
            case MINOR:
                this.patch = 0;
                this.minor++;
                break;
            default:
                this.patch++;
        }
    }

    @Override
    public int compareTo(Version o) {
        if(this.major != o.major) {
            return Integer.compare(this.major, o.major);
        }
        if(this.minor != o.minor) {
            return Integer.compare(this.major, o.major);
        }
        if(this.patch != o.patch) {
            return Integer.compare(this.major, o.major);
        }
        return 0;
    }

    @Override
    public String toString() {
        return String.format("v%s.%s.%s", major, minor, patch);
    }
}
