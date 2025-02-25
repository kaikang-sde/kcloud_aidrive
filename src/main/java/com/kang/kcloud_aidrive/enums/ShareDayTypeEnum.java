package com.kang.kcloud_aidrive.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * type: 0 - permanent, 1 - 7 days, 2 - 30 days
 *
 * @author Kai Kang
 */

@AllArgsConstructor
@Getter
public enum ShareDayTypeEnum {
    PERMANENT(0, 0),

    SEVEN_DAYS(1, 7),

    THIRTY_DAYS(2, 30);

    private Integer dayType;

    private Integer days;

    /**
     * get sharing days based on day type
     *
     * @param dayType 0/1/2
     * @return days
     */
    public static Integer getDaysByType(Integer dayType) {
        for (ShareDayTypeEnum value : ShareDayTypeEnum.values()) {
            if (value.getDayType().equals(dayType)) {
                return value.getDays();
            }
        }
        // default: 7 days
        return SEVEN_DAYS.days;
    }


}
