/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.weathervalues.internal;

import java.util.Objects;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;

/**
 * The {@link Wind}
 *
 * @author Thomas Traunbauer - Initial contribution
 */
public class Wind {

    private String value;
    private Double speed;

    public Wind(Integer directionInDeg, Double speedInKmh) throws IllegalArgumentException {
        if (directionInDeg != null) {
            if (directionInDeg < 0 || directionInDeg > 360) {
                throw new IllegalArgumentException("directionInDeg is not in degree");
            }
            if (directionInDeg == 360) {
                directionInDeg = 0;
            }
            if (directionInDeg >= 0 && directionInDeg < 45) {
                this.value = "North";
            } else if (directionInDeg >= 45 && directionInDeg < 90) {
                this.value = "NorthEast";
            } else if (directionInDeg >= 90 && directionInDeg < 135) {
                this.value = "East";
            } else if (directionInDeg >= 135 && directionInDeg < 180) {
                this.value = "SouthEast";
            } else if (directionInDeg >= 180 && directionInDeg < 225) {
                this.value = "South";
            } else if (directionInDeg >= 225 && directionInDeg < 270) {
                this.value = "SouthWest";
            } else if (directionInDeg >= 270 && directionInDeg < 315) {
                this.value = "West";
            } else if (directionInDeg >= 315 && directionInDeg < 360) {
                this.value = "NorthWest";
            }
        } else {
            this.value = null;
        }
        this.speed = speedInKmh;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof String) {
            return obj.equals(value);
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Wind other = (Wind) obj;
        return Objects.equals(this.value, other.value);
    }

    public State getDirection() {
        if (value != null) {
            return new StringType(value);
        }
        return UnDefType.NULL;
    }

    public State getSpeed() {
        if (speed != null) {
            double value = Utility.round(speed);
            return new DecimalType(value);
        }
        return UnDefType.NULL;
    }
}
